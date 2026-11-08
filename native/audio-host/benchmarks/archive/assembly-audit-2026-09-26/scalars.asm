00000001400b93d0 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)>:
1400b93d0:     	pushq	%rsi
1400b93d1:     	subq	$0x130, %rsp            # imm = 0x130
1400b93d8:     	movaps	%xmm6, 0x120(%rsp)
1400b93e0:     	movq	0x38(%rcx), %rsi
1400b93e4:     	movq	(%rsi), %rax
1400b93e7:     	movq	0x13e0(%rax), %r8
1400b93ee:     	movss	0x48(%r8), %xmm0
1400b93f4:     	ucomiss	0xbf545(%rip), %xmm0    # 0x140178940 <.refptr._newmode+0x58b8>
1400b93fb:     	jne	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b93fd:     	jp	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b93ff:     	movq	0x88b0(%rax), %r8
1400b9406:     	movss	0x48(%r8), %xmm0
1400b940c:     	ucomiss	0xbf531(%rip), %xmm0    # 0x140178944 <.refptr._newmode+0x58bc>
1400b9413:     	jne	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b9415:     	jp	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b9417:     	movq	0x9c28(%rax), %r8
1400b941e:     	movss	0x44(%r8), %xmm0
1400b9424:     	xorps	%xmm1, %xmm1
1400b9427:     	ucomiss	%xmm1, %xmm0
1400b942a:     	jne	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b942c:     	jp	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b942e:     	movq	0x7538(%rax), %r8
1400b9435:     	movss	0x44(%r8), %xmm0
1400b943b:     	ucomiss	0xbf4fe(%rip), %xmm0    # 0x140178940 <.refptr._newmode+0x58b8>
1400b9442:     	jne	0x1400b9446 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x76>
1400b9444:     	jnp	0x1400b945b <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x8b>
1400b9446:     	movaps	0x120(%rsp), %xmm6
1400b944e:     	addq	$0x130, %rsp            # imm = 0x130
1400b9455:     	popq	%rsi
1400b9456:     	jmp	0x1400b58d0 <easyknack::MultipleGraphNode::Instance::renderScalars(int)>
1400b945b:     	xorps	%xmm0, %xmm0
1400b945e:     	movaps	%xmm0, 0x20(%rsp)
1400b9463:     	leaq	0x20(%rsp), %rcx
1400b9468:     	movq	%rcx, 0x30(%rsp)
1400b946d:     	leaq	0x24(%rsp), %rdx
1400b9472:     	movq	%rdx, 0x38(%rsp)
1400b9477:     	leaq	0x28(%rsp), %rdx
1400b947c:     	movq	%rdx, 0x40(%rsp)
1400b9481:     	leaq	0x2c(%rsp), %rdx
1400b9486:     	movq	%rdx, 0x48(%rsp)
1400b948b:     	movaps	%xmm0, 0x50(%rsp)
1400b9490:     	movq	%rcx, 0x60(%rsp)
1400b9495:     	movq	$0x1, 0x68(%rsp)
1400b949e:     	movsd	0x490(%rsi), %xmm1
1400b94a6:     	movsd	%xmm1, 0x70(%rsp)
1400b94ac:     	leaq	0x88(%rsi), %rcx
1400b94b3:     	movq	%rcx, 0x78(%rsp)
1400b94b8:     	movaps	%xmm0, 0x80(%rsp)
1400b94c0:     	movq	$0x0, 0x90(%rsp)
1400b94cc:     	movups	%xmm0, 0xa8(%rsp)
1400b94d4:     	movups	%xmm0, 0xb8(%rsp)
1400b94dc:     	movups	%xmm0, 0xc8(%rsp)
1400b94e4:     	movups	%xmm0, 0xd8(%rsp)
1400b94ec:     	movups	%xmm0, 0xe3(%rsp)
1400b94f4:     	movups	%xmm0, 0xf8(%rsp)
1400b94fc:     	movups	%xmm0, 0x108(%rsp)
1400b9504:     	leaq	0x30(%rsp), %rcx
1400b9509:     	movq	%rcx, 0x98(%rsp)
1400b9511:     	movq	$0x4, 0xa0(%rsp)
1400b951d:     	movq	0x68(%rax), %rcx
1400b9521:     	leaq	0x50(%rsp), %rdx
1400b9526:     	callq	0x1400aec50 <easyknack::InstanceContextNode::process(easyknack::ProcessContext&)>
1400b952b:     	movss	0x20(%rsp), %xmm0
1400b9531:     	addss	0xbf407(%rip), %xmm0    # 0x140178940 <.refptr._newmode+0x58b8>
1400b9539:     	movd	%xmm0, %eax
1400b953d:     	andl	$0x7fffffff, %eax       # imm = 0x7FFFFFFF
1400b9542:     	cmpl	$0x7f800000, %eax       # imm = 0x7F800000
1400b9547:     	jl	0x1400b954c <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x17c>
1400b9549:     	xorps	%xmm0, %xmm0
1400b954c:     	movq	(%rsi), %rsi
1400b954f:     	movq	0xea08(%rsi), %rax
1400b9556:     	movq	0x38(%rax), %rcx
1400b955a:     	testq	%rcx, %rcx
1400b955d:     	je	0x1400b9572 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x1a2>
1400b955f:     	cmpq	$0x2, 0x8(%rcx)
1400b9564:     	jb	0x1400b9572 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x1a2>
1400b9566:     	movq	(%rcx), %rcx
1400b9569:     	movq	0x8(%rcx), %rcx
1400b956d:     	testq	%rcx, %rcx
1400b9570:     	jne	0x1400b9579 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x1a9>
1400b9572:     	addq	$0x68, %rax
1400b9576:     	movq	%rax, %rcx
1400b9579:     	movss	(%rcx), %xmm1
1400b957d:     	cvtss2sd	%xmm1, %xmm1
1400b9581:     	movq	%xmm1, %rdx
1400b9586:     	movabsq	$0x7fffffffffffffff, %rax # imm = 0x7FFFFFFFFFFFFFFF
1400b9590:     	andq	%rax, %rdx
1400b9593:     	movabsq	$0x7fefffffffffffff, %rcx # imm = 0x7FEFFFFFFFFFFFFF
1400b959d:     	xorps	%xmm6, %xmm6
1400b95a0:     	cmpq	%rcx, %rdx
1400b95a3:     	jg	0x1400b95d5 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x205>
1400b95a5:     	movsd	0xbf39b(%rip), %xmm2    # 0x140178948 <.refptr._newmode+0x58c0>
1400b95ad:     	minsd	%xmm1, %xmm2
1400b95b1:     	movsd	0xbf397(%rip), %xmm3    # 0x140178950 <.refptr._newmode+0x58c8>
1400b95b9:     	cmpltsd	%xmm3, %xmm1
1400b95be:     	movapd	%xmm1, %xmm4
1400b95c2:     	andnpd	%xmm2, %xmm4
1400b95c6:     	andpd	%xmm3, %xmm1
1400b95ca:     	orpd	%xmm4, %xmm1
1400b95ce:     	xorps	%xmm6, %xmm6
1400b95d1:     	cvtsd2ss	%xmm1, %xmm6
1400b95d5:     	mulss	%xmm0, %xmm6
1400b95d9:     	movd	%xmm6, %edx
1400b95dd:     	andl	$0x7fffffff, %edx       # imm = 0x7FFFFFFF
1400b95e3:     	cmpl	$0x7f800000, %edx       # imm = 0x7F800000
1400b95e9:     	jl	0x1400b95ee <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x21e>
1400b95eb:     	xorps	%xmm6, %xmm6
1400b95ee:     	movq	0xfd80(%rsi), %rdx
1400b95f5:     	movq	0x38(%rdx), %r8
1400b95f9:     	testq	%r8, %r8
1400b95fc:     	je	0x1400b9611 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x241>
1400b95fe:     	cmpq	$0x3, 0x8(%r8)
1400b9603:     	jb	0x1400b9611 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x241>
1400b9605:     	movq	(%r8), %r8
1400b9608:     	movq	0x10(%r8), %r8
1400b960c:     	testq	%r8, %r8
1400b960f:     	jne	0x1400b9618 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x248>
1400b9611:     	addq	$0x68, %rdx
1400b9615:     	movq	%rdx, %r8
1400b9618:     	movss	(%r8), %xmm1
1400b961d:     	cvtss2sd	%xmm1, %xmm1
1400b9621:     	movq	%xmm1, %rdx
1400b9626:     	andq	%rax, %rdx
1400b9629:     	cmpq	%rcx, %rdx
1400b962c:     	jle	0x1400b9638 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x268>
1400b962e:     	movss	0xbf322(%rip), %xmm1    # 0x140178958 <.refptr._newmode+0x58d0>
1400b9636:     	jmp	0x1400b966c <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x29c>
1400b9638:     	movsd	0xbf308(%rip), %xmm2    # 0x140178948 <.refptr._newmode+0x58c0>
1400b9640:     	minsd	%xmm1, %xmm2
1400b9644:     	movsd	0xbf304(%rip), %xmm3    # 0x140178950 <.refptr._newmode+0x58c8>
1400b964c:     	cmpltsd	%xmm3, %xmm1
1400b9651:     	movapd	%xmm1, %xmm4
1400b9655:     	andnpd	%xmm2, %xmm4
1400b9659:     	andpd	%xmm3, %xmm1
1400b965d:     	orpd	%xmm4, %xmm1
1400b9661:     	cvtsd2ss	%xmm1, %xmm1
1400b9665:     	xorps	0xbf2f4(%rip), %xmm1    # 0x140178960 <.refptr._newmode+0x58d8>
1400b966c:     	movd	%xmm1, %eax
1400b9670:     	andl	$0x7fffffff, %eax       # imm = 0x7FFFFFFF
1400b9675:     	xorpd	%xmm2, %xmm2
1400b9679:     	addss	%xmm2, %xmm1
1400b967d:     	movd	%xmm1, %ecx
1400b9681:     	andl	$0x7fffffff, %ecx       # imm = 0x7FFFFFFF
1400b9687:     	cmpl	$0x7f800000, %ecx       # imm = 0x7F800000
1400b968d:     	jl	0x1400b9692 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x2c2>
1400b968f:     	xorps	%xmm1, %xmm1
1400b9692:     	cmpl	$0x7f800000, %eax       # imm = 0x7F800000
1400b9697:     	jl	0x1400b969c <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x2cc>
1400b9699:     	xorps	%xmm1, %xmm1
1400b969c:     	callq	0x140171b90 <powf>
1400b96a1:     	movd	%xmm0, %eax
1400b96a5:     	andl	$0x7fffffff, %eax       # imm = 0x7FFFFFFF
1400b96aa:     	cmpl	$0x7f800000, %eax       # imm = 0x7F800000
1400b96af:     	jl	0x1400b96b4 <easyknack::LocalBakedGroup6920b561e015::CompiledInstance::renderScalars(int)+0x2e4>
1400b96b1:     	xorps	%xmm0, %xmm0
1400b96b4:     	movq	0x3a38(%rsi), %rax
1400b96bb:     	movq	(%rax), %rax
1400b96be:     	movq	(%rax), %rax
1400b96c1:     	movss	%xmm6, (%rax)
1400b96c5:     	movq	0x8818(%rsi), %rax
1400b96cc:     	movq	(%rax), %rax
1400b96cf:     	movq	(%rax), %rax
1400b96d2:     	movss	%xmm0, (%rax)
1400b96d6:     	movaps	0x120(%rsp), %xmm6
1400b96de:     	addq	$0x130, %rsp            # imm = 0x130
1400b96e5:     	popq	%rsi
1400b96e6:     	retq
1400b96e7:     	int3
1400b96e8:     	int3
1400b96e9:     	int3
1400b96ea:     	int3
1400b96eb:     	int3
1400b96ec:     	int3
1400b96ed:     	int3
1400b96ee:     	int3
1400b96ef:     	int3

